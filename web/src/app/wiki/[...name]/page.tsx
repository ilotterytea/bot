"use client";

import { wikiPage } from "../page";

export default async function Page({params}: {params: {name: string[]}}) {
    return await wikiPage(params.name.join("/"));
}