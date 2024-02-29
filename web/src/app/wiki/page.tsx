"use client";

import { faArrowDown, faBook } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Skeleton } from "@nextui-org/react";
import { MDXRemote } from "next-mdx-remote";
import { serialize } from "next-mdx-remote/serialize";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    return wikiPage("README");
}

export function wikiPage(name: string): JSX.Element {
    const [summary, setSummary] = useState();
    const [content, setContent] = useState();
    const [sidebarVisible, setSidebarVisible] = useState(true);
    const summaryLoaded = useRef(false);
    const contentLoaded = useRef(false);

    useEffect(() => {
        const serializeSource = async (str: string) => {
            return await serialize(str, {
                mdxOptions: {
                    development: process.env.NODE_ENV === "development"
                }
            });
        }

        if (!summaryLoaded.current) {
            fetch("http://0.0.0.0:8085/v1/docs/summary")
                .then((response) => response.json())
                .then((json) => {

                    serializeSource(json.data.content)
                        .then((data) => {
                            setSummary(data);
                            summaryLoaded.current = true;
                        });
                })
                .catch((err) => console.error(err));
        }

        if (!contentLoaded.current) {
            fetch("http://0.0.0.0:8085/v1/docs/" + name)
                .then((response) => response.json())
                .then((json) => {
                    serializeSource((json.status_code !== 200) ? json.message : json.data.content)
                        .then((data) => {
                            setContent(data);
                            contentLoaded.current = true;
                        });
                })
                .catch((err) => console.error(err));
        }
    }, []);


    const addWindowListener = () => {
        if (window) {
            window
                .matchMedia("(min-width: 1024px)")
                .addEventListener('change', e => setSidebarVisible(e.matches));
        } else {
            setTimeout(addWindowListener, 1000);
        }
    };

    useEffect(() => {
        addWindowListener();
    }, []);

    return (
        <main className="w-full min-h-screen flex flex-col shadow-lg bg-stone-100 dark:bg-stone-900">
            <div className="bg-gradient-to-t from-emerald-300 to-teal-200 p-2 pl-8 shadow-lg">
                <h1 className="text-2xl font-anta">
                    <FontAwesomeIcon icon={faBook} /> Wiki
                </h1>
            </div>
            <div className="w-full min-h-screen flex flex-col lg:flex-row">
                {
                    // Sidebar
                    summaryLoaded.current
                    ? (
                        <div className="sidebar flex flex-col lg:sticky lg:top-0 overflow-y-scroll px-6 lg:max-h-[100vh] bg-stone-200 dark:bg-stone-800">
                            <Button className="lg:hidden my-4 text-left flex justify-between" onClick={() => setSidebarVisible(!sidebarVisible)}>
                                PAGES
                                <FontAwesomeIcon icon={faArrowDown} />
                            </Button>
                            {
                                sidebarVisible ?
                                <>
                                    <p className="font-anta mt-2">PAGES</p>
                                    <MDXRemote {...summary} components={{}} />
                                </>
                                :
                                <></>
                            }
                        </div>
                    )
                    : (
                        <Skeleton>
                            <div className="h-12 w-full rounded-lg bg-default-300" />
                        </Skeleton>
                    )
                }
                
                <div className="wiki-content">
                    {
                        // Main content
                        contentLoaded.current
                        ? (
                            <MDXRemote {...content} components={{}} />
                        )
                        : (
                            <div className="space-y-2 my-5">
                                <Skeleton className="rounded-lg mb-5">
                                    <div className="h-12 w-full rounded-lg bg-default-300" />
                                </Skeleton>
                                {Array.from({length: 25}).map((_, i) => (
                                    <Skeleton className="rounded-lg" key={i}>
                                        <div className="h-6 w-full rounded-lg bg-default-300" />
                                    </Skeleton>
                                ))}
                            </div>
                        )
                    }
                </div>
            </div>
        </main>
    );
}