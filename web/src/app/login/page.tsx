"use client";

import Navbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { faCheck, faCircleNotch, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Cookies, useCookies } from "next-client-cookies";
import { AppRouterInstance } from "next/dist/shared/lib/app-router-context.shared-runtime";
import Link from "next/link";
import { ReadonlyURLSearchParams, useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    const router = useRouter();
    const cookies = useCookies();
    const params = useSearchParams();

    const code = params.has("code");
    const scope = params.has("scope");
    const state = params.has("state");

    const code_condition = !code || !scope || !state;

    return (
        <main className="w-full min-h-screen flex flex-col animate-mainscreen bg-gradient-to-b from-neutral-50 dark:from-neutral-950 via-teal-50 dark:via-teal-950 to-neutral-100 dark:to-neutral-900">
            <Navbar />
            <div className="w-full grow flex flex-row pt-16 justify-center items-center">
                <div className="flex items-center flex-row space-x-4 p-4 bg-neutral-100 dark:bg-neutral-900 border-neutral-200 dark:border-neutral-800 border-2 rounded-lg">
                    {
                        code_condition ? retrieveTwitchCode(router) :
                        authorizeTwitchCode(router, params, cookies)
                    }
                </div>
            </div>
            <SmallFooter />
        </main>
    );
}

function retrieveTwitchCode(router: AppRouterInstance) {
    const init = useRef(false);

    useEffect(() => {
        if (!init.current) {
            init.current = true;

            fetch("http://localhost:8085/v1/authenticate", {
                method: "POST",
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    "scopes": []
                })
            })
                .then((response) => response.json())
                .then((json) => {
                    router.push(json.data.url);
                })
                .catch((err) => {
                    console.error(err);

                    return (
                        <>
                            <div className="text-4xl">
                                <FontAwesomeIcon
                                    icon={faXmark}
                                />
                            </div>
                            <div>
                                <h1 className="text-xl">Failed to log in! Try again later.</h1>
                                <p className="text-sm">See the console for more information</p>
                            </div>
                        </>
                    );
                });
        }
    }, []);

    return (
        <>
            <div className="text-4xl animate-spin">
                <FontAwesomeIcon
                    icon={faCircleNotch}
                />
            </div>
            <div>
                <h1 className="text-xl">Making the authorization link ready for you...</h1>
                <p className="text-sm">You'll be automatically redirected</p>
            </div>
        </>
    );
}

function authorizeTwitchCode(router: AppRouterInstance, params: ReadonlyURLSearchParams, cookies: Cookies) {
    const code = params.get("code");
    const scope = params.get("scope");
    const state = params.get("state");

    const [data, setData] = useState();
    const initialized = useRef(false);

    useEffect(() => {
        if (!initialized.current) {
            initialized.current = true;


            fetch(`http://localhost:8085/v1/authenticate?code=${code}&scope=${scope}&state=${state}`)
            .then((response) => response.json())
            .then((json) => {
                const data = json.data;

                setData(data);

                const settings = {
                    expires: new Date(data.expires_at + "Z")
                };

                cookies.set("token", data.token, settings);
                cookies.set("client_id", data.client_id, settings);
            }).catch((err) => {
                console.error(err);

                return (
                    <>
                        <div className="text-4xl">
                            <FontAwesomeIcon
                                icon={faXmark}
                            />
                        </div>
                        <div>
                            <h1 className="text-xl">Failed to log in! Try again later.</h1>
                            <p className="text-sm">See the console for more information</p>
                        </div>
                    </>
                );
            });
        }
    }, []);


    if (data) {
        return (
            <>
                <div className="text-4xl mx-4">
                    <FontAwesomeIcon
                        icon={faCheck}
                    />
                </div>
                <div>
                    <h1 className="text-xl">Welcome home, {data.user.alias_name}</h1>
                    <Link href="/dashboard">
                        <div className="w-full flex justify-center items-center p-2 mt-3 bg-teal-200 dark:bg-teal-400 hover:bg-teal-300 dark:hover:bg-teal-500 border-teal-300 dark:border-teal-500 hover:border-teal-400 dark:hover:border-teal-600 border-2 rounded-lg transition">
                            <p>Go to dashboard</p>
                        </div>
                    </Link>
                </div>
            </>
        );
    } else {
        return (
            <>
                <div className="text-4xl animate-spin">
                    <FontAwesomeIcon
                        icon={faCircleNotch}
                    />
                </div>
                <div>
                    <h1 className="text-xl">We're identifying you...</h1>
                    <p className="text-sm">Almost there</p>
                </div>
            </>
        )
    }
}