import Image from "next/image";

const Footer = (): JSX.Element => (
    <div className="w-full flex justify-center py-16 bg-gradient-to-t from-neutral-200 dark:from-zinc-900 to-neutral-300 dark:to-zinc-950">
        <div className="w-full px-16 lg:w/2 flex flex-row justify-between">
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
                            <h2 className="text-2xl font-medium">iLotterytea's</h2>
                            <h1 className="text-3xl font-semibold">Twitch bot</h1>
                        </div>
                    </div>
                </div>
                <p>
                    Maintained and developed by <a href="https://ilotterytea.kz" className="text-teal-500 hover:text-teal-800 transition">@ilotterytea</a>
                </p>
            </div>
        </div>
    </div>
);

export default Footer;