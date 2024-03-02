"use client";

import { useWikiPageGenerator } from "../utils";

export default function Page({params}: {params: {name: string[]}}) {
    return useWikiPageGenerator(params.name.join("/"));
}