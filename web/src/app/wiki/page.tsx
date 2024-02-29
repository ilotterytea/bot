"use client";

import { faBook } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Skeleton } from "@nextui-org/react";
import { MDXRemote } from "next-mdx-remote";
import { serialize } from "next-mdx-remote/serialize";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    return wikiPage("README");
}

export function wikiPage(name: string): JSX.Element {
    const [summary, setSummary] = useState();
    const [content, setContent] = useState();
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

    return (
        <main className="w-full min-h-screen flex flex-col shadow-lg bg-stone-100 dark:bg-stone-900">
            <div className="bg-gradient-to-t from-emerald-300 to-teal-200 p-2 pl-8 shadow-lg">
                <h1 className="text-2xl font-anta">
                    <FontAwesomeIcon icon={faBook} /> Wiki
                </h1>
            </div>
            <div className="w-full min-h-screen grid grid-cols-[250px_1fr]">
                {
                    // Sidebar
                    summaryLoaded.current
                    ? (
                        <div className="sidebar bg-stone-200 dark:bg-stone-800">
                            <p className="font-anta mt-2">PAGES</p>
                            <MDXRemote {...summary} components={{}} />
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