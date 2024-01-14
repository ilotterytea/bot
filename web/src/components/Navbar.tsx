import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Avatar, Dropdown, DropdownItem, DropdownMenu, DropdownSection, DropdownTrigger, Navbar, NavbarBrand, NavbarContent, NavbarItem, User } from "@nextui-org/react";
import { useCookies } from "next-client-cookies";
import Image from "next/image";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";

const AppNavbar = () : JSX.Element => {
    const cookies = useCookies();

    const [channels, setChannels] = useState([]);
    const [channelIndex, setChannelIndex] = useState(0);
    const loaded = useRef(false);

    useEffect(() => {
       if (!loaded.current) {
        const moderated_channels = cookies.get("ttv_moderated_channels");
        const moderating_channel = cookies.get("ttv_moderating_index");

        if (moderated_channels && moderating_channel) {
            setChannels(JSON.parse(moderated_channels));
            setChannelIndex(JSON.parse(moderating_channel));

            loaded.current = true;
        }
       }
    });

    return (
        <Navbar isBordered>
            <NavbarBrand>
                <Avatar
                    src={"/bot_avatar.png"}
                    radius="lg"
                />
            </NavbarBrand>
            <NavbarContent className="hidden sm:flex gap-4" justify="center">
                <NavbarItem>
                    <Link href={"/"}>
                        Home
                    </Link>
                </NavbarItem>
            </NavbarContent>
        </Navbar>
    );

    return (
        <div className="w-full h-16 flex justify-around items-center text-lg fixed dark:bg-neutral-900 dark:bg-opacity-50 backdrop-blur-xl">
            {
                // Logo
            }
            <div className="flex h-full p-2">
                
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
                {
                    !loaded.current ? (
                        <a href="/login" >
                            <div className="transition px-4 py-2 rounded-full hover:bg-teal-50 text-teal-800 dark:text-teal-200 hover:text-teal-950">
                                <p><FontAwesomeIcon
                                        icon={faRightFromBracket}
                                    /> Log in...</p>
                            </div>
                        </a>
                    ) :
                    (
                        <Dropdown>
                            <DropdownTrigger>
                                <Avatar
                                    src={channels[channelIndex].profile_image_url}
                                    radius="full"
                                    size="md"
                                />
                            </DropdownTrigger>
                            <DropdownMenu>
                                <DropdownSection title={"Signed in as " + channels[channelIndex].login} showDivider={true}>
                                    <DropdownItem title="Dashboard" href="/dashboard" />
                                </DropdownSection>
                                {
                                    !channels || channels.length === 1 ?
                                    (<></>) :
                                    (
                                        <DropdownSection showDivider={true}>
                                            {
                                                channels.map((value, index) => 
                                                index !== channelIndex ?
                                                (<DropdownItem key={index}>
                                                    <User
                                                        name={value.login}
                                                        avatarProps={{
                                                            src: value.profile_image_url,
                                                            size: "sm"
                                                        }}
                                                        onClick={e => {
                                                            setChannelIndex(index);
                                                            cookies.set("ttv_moderating_index", index.toString());
                                                        }}
                                                    />
                                                </DropdownItem>)
                                                // uuuuh
                                                : (<DropdownItem className="hidden" />)
                                                )
                                            }
                                        </DropdownSection>
                                    )
                                }
                                <DropdownSection>
                                    <DropdownItem title={"Settings"} />
                                    <DropdownItem title={"Log out"} color="danger"/>
                                </DropdownSection>
                            </DropdownMenu>
                        </Dropdown>
                    )
                }
            </div>
        </div>
    );
}

const NavbarButton = ({stylized_name, url}: {stylized_name: string, url: string}): JSX.Element => (
    <a href={url} >
        <div className="transition px-4 py-2 rounded-full hover:bg-neutral-50 dark:hover:bg-neutral-900 text-neutral-600 dark:text-neutral-300 hover:text-neutral-900 dark:hover:text-neutral-50">
            {stylized_name}
        </div>
    </a>
);

export default AppNavbar;