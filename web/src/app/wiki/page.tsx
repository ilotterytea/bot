import { Metadata } from "next";
import WikiPage from "./wiki";

export const metadata: Metadata = {
    title: "wiki"
};

export default function Page() {
    return <><WikiPage page_id="README" /></>;
}