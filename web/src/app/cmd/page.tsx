import Navbar from "@/components/Navbar";

export interface CommandItemData {
    name_id: string,
    styled_name: string,
    short: string,
}

export default async function Page() {
    const response = await fetch("http://0.0.0.0:8085/v1/docs/commands");
    const response_json = await response.json();
    const items: CommandItemData[] = response_json.data;

    return (
        <main>
            <Navbar />
            <div className="flex flex-col w-screen h-screen">
                    <div className="my-8 flex justify-center">
                        <h1 className="text-teal-500 font-semibold text-3xl">Commands</h1>
                    </div>
                    
                    {
                        // Items
                    }

                    <div className="bg-gray-100 w-full h-full flex flex-col items-center">
                        <div className="w-3/4 my-2">
                            {
                                items.map((item, index) => (
                                    <CommandItem item={item} key={index} />
                                ))
                            }
                        </div>
                    </div>
            </div>
        </main>
    )
}

export const CommandItem = (
    {item}: {item: CommandItemData}
): JSX.Element => (
    <a href={"/cmd/" + item.name_id} className="w-full">
        <div className="flex flex-row items-center h-10 w-full border-b-2 border-slate-300 hover:bg-slate-200 p-2">
            <h1 className="w-1/4 text-ellipsis text-teal-500 font-bold">
                {item.styled_name}
            </h1>
            <p className="text-ellipsis">
                {item.short}
            </p>
        </div>
    </a>
)