import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import Image from "next/image";

const Navbar = () : JSX.Element => (
    <div className="w-full h-16 flex justify-around items-center text-lg fixed dark:bg-neutral-900 dark:bg-opacity-50 backdrop-blur-xl">
        {
            // Logo
        }
        <div className="flex h-full p-2">
            <Image
                alt={"@imteabot"}
                src={"/bot_avatar.png"}
                width={128}
                height={128}

                className="rounded-lg h-full w-fit"
            />
        </div>

        {
            // Quick buttons
        }
        <div className="flex h-full space-x-6 items-center">
            <NavbarButton stylized_name="Home" url="/" />
            <NavbarButton stylized_name="FAQ" url="/faq" />
            <NavbarButton stylized_name="Wiki" url="/wiki" />
            <NavbarButton stylized_name="Statistics" url="/stats" />
        </div>

        {
            // Log in
        }
        <div className="flex h-full items-center">
            <a href="/login" >
                <div className="transition px-4 py-2 rounded-full hover:bg-teal-50 text-teal-800 dark:text-teal-200 hover:text-teal-950">
                    <p><FontAwesomeIcon
                            icon={faRightFromBracket}
                        /> Log in...</p>
                </div>
            </a>
        </div>
    </div>
);

const NavbarButton = ({stylized_name, url}: {stylized_name: string, url: string}): JSX.Element => (
    <a href={url} >
        <div className="transition px-4 py-2 rounded-full hover:bg-neutral-50 dark:hover:bg-neutral-900 text-neutral-600 dark:text-neutral-300 hover:text-neutral-900 dark:hover:text-neutral-50">
            {stylized_name}
        </div>
    </a>
);

export default Navbar;