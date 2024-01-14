"use client";

import AppNavbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { faExternalLink } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import Image from "next/image";
import Link from "next/link";

export default function Page() {
    return (
        <main className="w-full min-h-screen flex flex-col">
            <AppNavbar />
            <div className="w-full grow flex flex-col justify-center items-center">
                <div className="space-y-4">
                    <div className="flex justify-center items-center">
                        <Image
                            src={"/under-construction.png"}
                            width={"96"}
                            height={"96"}
                            alt={"🏗️"}
                        />
                    </div>
                    <div className="flex flex-col justify-center items-center">
                        <h1 className="font-semibold text-3xl">This website is under construction</h1>
                        <p className="text-xl">
                            Keep an eye on <Link href={"https://git.ilotterytea.kz/tea/bot"} className="text-teal-500 hover:text-teal-600 transition-colors">the repository <FontAwesomeIcon icon={faExternalLink} className="text-sm"/></Link> for updates
                        </p>
                    </div>
                </div>
            </div>
            <SmallFooter />
        </main>
    )
}