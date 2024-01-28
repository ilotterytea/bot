"use client";

import AppNavbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { Avatar, Card, CardBody, CardFooter, Image, Spinner, Tab, Tabs } from "@nextui-org/react";

export default function Page() {
    const events = [
        {
            alias_id: 191400264,
            alias_name: "ilotterytea",
            event_type: "live"
        },
        {
            alias_id: 191400264,
            alias_name: "ilotterytea",
            event_type: "live"
        },
        {
            alias_id: 191400264,
            alias_name: "ilotterytea",
            event_type: "live"
        },
        {
            alias_id: 191400264,
            alias_name: "ilotterytea",
            event_type: "live"
        },
        {
            alias_id: 191400264,
            alias_name: "ilotterytea",
            event_type: "live"
        },
    ];

    const tabs = [
        {
            name: "Events",
            content: (<EventListComponent data={events} />)
        }
    ];

    return (
        <main>
            <AppNavbar />
            <div className="w-full min-h-screen flex flex-row justify-center align-center text-lg space-y-64">
                <div className="w-full px-6 xl:px-0 xl:w-[50%] flex flex-col">
                    <div className="w-full flex flex-row py-4 space-x-4">
                        <Avatar
                            src="/bot_avatar.png"
                        />
                        <h1 className="text-4xl font-semibold">ilotterytea's dashboard</h1>
                    </div>
                    <div className="w-full flex flex-row grow space-x-4">
                        <Tabs color={"primary"} variant={"light"} aria-label="Dashboard options" classNames={{
                            tabList: "flex-col w-full",
                            base: "min-w-[20%]",
                            panel: "p-0 grow",
                            tab: "justify-start"
                        }}>
                            {
                                tabs.map((v) => (
                                    <Tab key={v.name} title={v.name}>
                                        <Card >
                                            <CardBody>
                                                {v.content}
                                            </CardBody>
                                        </Card>
                                    </Tab>
                                ))
                            }
                        </Tabs>
                    </div>
                </div>
            </div>
            <SmallFooter />
        </main>
    );
}

const EventListComponent = ({data}: {data: any[] | null}): JSX.Element => {
    return data ?
        (
            <div className="grid grid-cols-3 xl:grid-cols-5 gap-4">
            {
                data.map((v, i) => (
                    <Card
                        isFooterBlurred
                        shadow="sm"
                        isPressable
                        onPress={() => console.log("hi")}
                    >
                        <Image
                            src={"/bot_avatar.png"}
                            alt={v.alias_name + "'s pfp"}
                            width="100%"
                            radius="lg"
                            shadow="sm"
                            className="w-full object-cover h-fit"
                        />
                        <CardFooter className="text-small justify-between bg-slate-900/50 border-white/20 border-1 overflow-hidden py-1 absolute rounded-large bottom-1 w-[calc(100%_-_8px)] shadow-small ml-1 z-10">
                            <p className="text-stone-100 font-medium">{v.alias_name}</p>
                            <p className="text-teal-100">{v.event_type}</p>
                        </CardFooter>
                    </Card>
                ))
            }
            </div>
        )
        :
        (
            <div className="flex flex-col justify-center items-center space-y-4 w-full py-4">
                <Spinner />
            </div>
        )
    ;
};