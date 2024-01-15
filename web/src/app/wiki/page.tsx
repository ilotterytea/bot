"use client";

import Footer from "@/components/Footer";
import AppNavbar from "@/components/Navbar";
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
        <main className="w-full min-h-screen flex flex-col">
            <AppNavbar />
            <div className="w-full h-full flex flex-row">
                {
                    // Sidebar
                    summaryLoaded.current
                    ? (
                        <div className="sidebar">
                            <MDXRemote {...summary} components={{}} />
                        </div>
                    )
                    : (
                        <Skeleton className="min-h-screen w-[25%] p-4 mb-5">
                            <div className="h-12 w-full rounded-lg bg-default-300" />
                        </Skeleton>
                    )
                }
                
                <div className="wiki-content flex justify-center">
                    <div className="w-full xl:w-[75%]">
                    {
                        // Main content
                        contentLoaded.current
                        ? (
                            <MDXRemote {...content} components={{}} />
                        )
                        : (
                            <div className="space-y-2">
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
            </div>
            <Footer />
        </main>
    );
}