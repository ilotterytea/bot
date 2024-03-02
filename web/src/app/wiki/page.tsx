"use client";

import { useWikiPageGenerator } from "./utils";

export default function Page() {
    return useWikiPageGenerator("README");
}