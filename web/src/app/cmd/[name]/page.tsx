import Navbar from "@/components/Navbar";
import { MDXRemote } from "next-mdx-remote/rsc";
import { CommandItem, CommandItemData } from "../page";

export default async function Page({params}: {params: {name: string}}) {
    const docs_response = await fetch("http://0.0.0.0:8085/v1/docs/commands");
    const docs_response_json = await docs_response.json();
    const docs_items: CommandItemData[] = docs_response_json.data;

    const md_response = await fetch("http://0.0.0.0:8085/v1/docs/commands/" + params.name);
    const md_response_json = await md_response.json();
    const md_items: string = md_response_json.data;

    return (
        <main>
            <Navbar />
            <div className="flex flex-row w-screen h-screen">
            
                    {
                        // Sidebar
                    }

                    <div className="bg-gray-100 max-w-64 w-full h-full flex flex-col border-r-2 border-r-gray-300">

                        <div className="w-full">
                            <div className="flex justify-center">
                                <h1 className="text-teal-500 font-semibold text-lg">Commands</h1>
                            </div>
                            {
                                docs_items.map((item, index) => (
                                    <CommandItem item={item} key={index} />
                                ))
                            }
                        </div>
                    </div>

                    {
                        // Command info
                    }
                    <div className="flex flex-col w-full h-full bg-gray-200 p-8 text-lg text-slate-950 command-info">
                        <MDXRemote source={md_items} />
                    </div>
            </div>
        </main>
    )
}