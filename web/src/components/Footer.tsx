import { faGit } from "@fortawesome/free-brands-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button } from "@nextui-org/react";
import Image from "next/image";
import Link from "next/link";

const Footer = (): JSX.Element => (
    <div className="w-full flex justify-center px-8 lg:px-0 py-16 mt-16 font-anta bg-gradient-to-t from-stone-50 dark:from-zinc-900 to-stone-200 dark:to-zinc-950">
        <div className="w-full lg:w-[50%] flex flex-col space-y-8 lg:space-y-0 lg:flex-row justify-between">
            {
                // Developer information
            }
            <div>
                <div className="flex flex-row items-center">
                    <Image
                        src={"/itb.png"}
                        width={54}
                        height={54}
                        style={{height: "100%", width: "auto"}}
                        alt="Logo"
                    />
                    <div>
                        <div className="ml-2">
                            <h2 className="text-2xl font-medium">iLotterytea&apos;s</h2>
                            <h1 className="text-3xl font-semibold">Twitch bot</h1>
                        </div>
                    </div>
                </div>
                <p className="mt-2">
                    Maintained and developed by <a href="https://ilotterytea.kz" className="text-teal-500 hover:text-teal-800 transition">@ilotterytea</a>
                </p>
            </div>

            {
                // Links
            }
            <div className="flex lg:justify-center items-center space-y-4">
                <div className="flex justify-end">
                    <Link href={"https://git.ilotterytea.kz/tea/bot"}>
                        <Button className="text-2xl min-w-0 p-2 hover:bg-teal-400 dark:hover:bg-teal-200 dark:hover:text-stone-900">
                            <FontAwesomeIcon icon={faGit} />
                        </Button>
                    </Link>
                </div>
            </div>
        </div>
    </div>
);

export default Footer;