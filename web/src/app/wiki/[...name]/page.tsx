import WikiPage from "../wiki";

export default function Page({params}: {params: {name: string[]}}) {
    return <><WikiPage page_id={params.name.join("/")}/></>;
}