"use client";

import Footer from "@/components/Footer";
import Navbar from "@/components/Navbar"
import { MDXRemote } from "next-mdx-remote/rsc";

export default async function Page() {
    return await wikiPage("README");
}

export async function wikiPage(name: string): Promise<JSX.Element> {
    const summary_md = await getDocContent("summary");
    const readme_md = await getDocContent(name);

    return (
        <main className="w-full min-h-screen flex flex-col">
            <Navbar />
            <div className="w-full h-full flex flex-row pt-16">
                {
                    // Sidebar
                }
                <div className="sidebar">
                    <MDXRemote source={summary_md} />
                </div>
                {
                    // Main content
                }
                <div className="wiki-content flex justify-center">
                    <div className="w-full xl:w-[75%]">
                        <MDXRemote source={readme_md} />
                    </div>
                </div>
            </div>
            <Footer />
        </main>
    );
}

export async function getDocContent(name: string): Promise<string> {
    const response = await fetch("http://0.0.0.0:8085/v1/docs/" + name);

    const response_json = await response.json();

    if (response_json.status_code != 200) {
        return response_json.message;
    }

    const content: string = response_json.data.content;

    return content;
}