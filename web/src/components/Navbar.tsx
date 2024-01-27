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
                <NavbarItem>
                    <Link href={"/wiki"}>
                        Wiki
                    </Link>
                </NavbarItem>
            </NavbarContent>

            <NavbarContent as={"div"} justify={"end"}>
                {
                    channels.length > 0 ?
                    (
                        
                            <Dropdown placement="bottom-end">
                                <DropdownTrigger>
                                    <Avatar
                                        isBordered
                                        as={"button"}
                                        color="primary"
                                        name={channels[channelIndex].login}
                                        src={channels[channelIndex].profile_image_url}
                                    />
                                </DropdownTrigger>
                                <DropdownMenu aria-label="Profile Actions" variant="flat">
                                    {
                                        channels.length > 1 ?
                                        (
                                            <DropdownSection title={"Moderated channels"}>
                                                {
                                                    channels.map((v, i) => {
                                                        return i != channelIndex ?
                                                        (
                                                            <DropdownItem key={v.login} onPress={() => {
                                                                setChannelIndex(i);
                                                                cookies.set("ttv_moderating_index", i.toString());
                                                            }}>
                                                                <User
                                                                    name={v.login}
                                                                    avatarProps={{
                                                                        src: v.profile_image_url,
                                                                        size: "sm"
                                                                    }}
                                                                    
                                                                />
                                                            </DropdownItem>
                                                        )
                                                        :
                                                        // uuuuhhhhh
                                                        (<DropdownItem className="hidden" />);
                                                    })
                                                }
                                            </DropdownSection>
                                        )
                                        :
                                        (<></>)
                                    }

                                    <DropdownSection title={"My account"}>
                                        <DropdownItem key={"settings"}>Settings</DropdownItem>
                                        <DropdownItem key={"logout"} color="danger">Log out</DropdownItem>
                                    </DropdownSection>
                                </DropdownMenu>

                            </Dropdown>
                    )
                    :
                    (<></>)
                }

            </NavbarContent>

        </Navbar>
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