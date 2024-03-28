"use client";

import { useWikiPageGenerator } from "./utils";

export default function WikiPage({ page_id }: { page_id: string }): JSX.Element {
    return useWikiPageGenerator(page_id);
}