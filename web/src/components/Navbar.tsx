"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
/**
 * @deprecated Use NavigationBar
 */
export function AppNavbar(): JSX.Element {
    return (<></>);
}

export function NavigationBar(): JSX.Element {
    const pathname = usePathname();

    const items = [
        {
            name: "home",
            url: "/"
        },
        {
            name: "wiki",
            url: "/wiki"
        }
    ];

    return (
        <div className="w-full flex justify-center items-center p-4 font-zilla font-normal">
            <div className="w-full lg:w-[50%] flex flex-row items-center">
                {
                    pathname != "/" ?
                    <Link href={"/"}>
                        <Image
                            src={"/itb.png"}
                            width={64}
                            height={64}
                            alt="itb2"
                            className="transition-all w-12 hover:w-16"
                        />
                    </Link>
                    :
                    <></>
                }          
                <div className="grow flex flex-row text-sm">
                    {
                        items.map((v, i) => (
                            <Link className="mx-4 my-2 transition-colors hover:text-teal-400" href={v.url} key={i}>
                                {v.name}
                            </Link>
                        ))
                    }
                </div>
            </div>
        </div>
    );
};
