"use client";

import { faGit } from "@fortawesome/free-brands-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button } from "@nextui-org/react";
import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";

const Footer = (): JSX.Element => {
    const pathName = usePathname();

    const links = [
        {
            url: "https://git.ilotterytea.kz/tea/bot",
            icon: faGit
        }
    ];

    if (pathName === "/") {
        return (
            <div className="w-full flex justify-center px-8 lg:px-0 py-16 mt-16 font-anta bg-gradient-to-t from-stone-50 dark:from-zinc-900 to-stone-200 dark:to-zinc-950">
                <div className="w-full lg:w-[50%] flex flex-col space-y-8 lg:space-y-0 lg:flex-row justify-between">
                    {
                        // Developer information
                    }
                    <div>
                        <div className="flex flex-row items-center">
                            <Image
                                src={"/itb.png"}
                                width={54}
                                height={54}
                                style={{ height: "100%", width: "auto" }}
                                alt="Logo"
                            />
                            <div>
                                <div className="ml-2">
                                    <h2 className="text-2xl font-medium">iLotterytea&apos;s</h2>
                                    <h1 className="text-3xl font-semibold">Twitch bot</h1>
                                </div>
                            </div>
                        </div>
                        <p className="mt-2">
                            Maintained and developed by <a href="https://ilotterytea.kz" className="text-teal-500 hover:text-teal-800">@ilotterytea</a>
                        </p>
                    </div>

                    {
                        // Links
                    }
                    <div className="flex lg:justify-center items-center space-y-4">
                        <div className="flex space-x-2 justify-end">
                            {
                                links.map((v, i) => (
                                    <Link href={v.url} key={i}>
                                        <Button className="text-2xl min-w-0 p-2 hover:bg-teal-400 dark:hover:bg-teal-200 dark:hover:text-stone-900">
                                            <FontAwesomeIcon icon={v.icon} />
                                        </Button>
                                    </Link>
                                ))
                            }
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className="w-full flex justify-center px-8 lg:px-0 py-1 mt-4 font-anta bg-gradient-to-t from-stone-50 dark:from-zinc-900 to-stone-200 dark:to-zinc-950">
            <div className="w-full lg:w-[50%] flex flex-col sm:flex-row justify-center items-center sm:justify-between">
                <p className="align-middle">
                    Powered by <Link href={"https://ilotterytea.kz"} className="text-teal-500 hover:text-teal-800">@ilotterytea</Link>. Licensed under WTFPL.
                </p>
                <div className="flex flex-row space-x-4">
                    {
                        links.map((v, i) => (
                            <Link href={v.url} key={i} className="text-2xl text-teal-700 dark:text-teal-300 hover:text-teal-500">
                                <FontAwesomeIcon icon={v.icon} />
                            </Link>
                        ))
                    }
                </div>
            </div>
        </div>
    )
};

export default Footer;