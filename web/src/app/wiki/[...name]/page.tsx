"use client";

import { wikiPage } from "../page";

export default function Page({params}: {params: {name: string[]}}) {
    return wikiPage(params.name.join("/"));
}