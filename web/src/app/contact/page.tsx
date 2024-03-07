import { faEnvelope, faExternalLink } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import Image from "next/image";
import Link from "next/link";

export default function Page() {
    const contactsEnv = process.env.NEXT_PUBLIC_CONTACTS;
    const contacts: { name: string, role: string | null, link: string | null }[] = [];

    let contactsJsx: JSX.Element;

    if (contactsEnv) {
        for (const contact of contactsEnv.split(",")) {
            const regex = /(.+?) \((.*?)\) <(.*?)>/;
            const [, name, role, link] = contact.match(regex) || [];

            contacts.push({
                name: name,
                role: role,
                link: link
            });
        }

        contactsJsx = <div className="w-full">
            {
                contacts.map((v, i) => (
                    <div className="m-4" key={"contacts-" + i}>
                        <Link
                            href={v.link ? v.link : "#"}
                            className="w-full flex flex-col lg:flex-row items-center p-4 space-x-4 rounded-lg hover:bg-teal-100 dark:hover:bg-teal-900 transition-colors"
                        >
                            <Image
                                src={"/contacts/" + v.name + ".png"}
                                width={80}
                                height={80}
                                alt="404"
                                className="bg-stone-200 dark:bg-stone-800 rounded-lg"
                            />
                            <div className="h-full flex flex-col justify-between">
                                <h1 className="text-4xl font-manrope font-semibold">{v.name} {v.link ? <FontAwesomeIcon icon={faExternalLink} className="text-sm" /> : ""}</h1>
                                {
                                    v.role ?
                                        <p className="text-xl text-stone-500">{v.role}</p>
                                        :
                                        <></>
                                }
                            </div>
                        </Link>
                    </div>
                ))
            }
        </div>;

    } else {
        contactsJsx = <div className="w-full my-8 flex justify-center items-center">
            <div className="space-y-4 lg:w-1/2 flex flex-col justify-center items-center">
                <Image
                    src={"/emojis/pensive.png"}
                    width={128}
                    height={128}
                    alt=""
                    className="bg-stone-200 dark:bg-stone-800 p-2 rounded-lg"
                />
                <h1 className="text-4xl font-manrope font-semibold">No contacts were found!</h1>
                <p className="text-stone-400 font-inter text-xl text-center">The author of <Link href={"https://git.ilotterytea.kz/tea/bot"} className="text-teal-300 hover:text-teal-400">&quot;ilotterytea&apos;s twitch bot&quot; project</Link> is not responsible for this instance of the bot.</p>
            </div>
        </div>;
    }

    return (
        <div className="w-full flex flex-col shadow-lg bg-stone-100 dark:bg-stone-900">
            <div className="bg-gradient-to-t from-emerald-300 dark:from-emerald-700 to-teal-200 dark:to-teal-600 p-2 pl-8 shadow-lg">
                <h1 className="text-2xl font-zilla font-semibold">
                    <FontAwesomeIcon icon={faEnvelope} /> Contacts
                </h1>
            </div>
            <div className="w-full">
                {contactsJsx}
            </div>
        </div>
    );
}