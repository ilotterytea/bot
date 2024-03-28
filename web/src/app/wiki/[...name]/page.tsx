import { Metadata } from "next";
import WikiPage from "../wiki";

export default function Page({params}: {params: {name: string[]}}) {
    return <><WikiPage page_id={params.name.join("/")}/></>;
}

export async function generateMetadata({
    params
}: {params: {name: string[]}}): Promise<Metadata> {
    const { name } = params;

    return {
        title: name[name.length - 1]
    }
}