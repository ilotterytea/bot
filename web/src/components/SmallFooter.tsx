import Link from "next/link";

export const SmallFooter = (): JSX.Element => (
    <div className="w-full flex justify-center py-4 dark:bg-neutral-900 dark:bg-opacity-50 backdrop-blur-xl">
        <div className="w-full px-16 lg:w/2 flex flex-row justify-between">
            {
                // Developer information
            }
            <div className="flex flex-row space-x-2">
                <Link href={"https://git.ilotterytea.kz/tea/bot"} className="text-teal-500 hover:text-teal-600 transition-colors">
                    Powered by ITB2.
                </Link>
                <p>Licensed under WTFPL.</p>
            </div>
        </div>
    </div>
);