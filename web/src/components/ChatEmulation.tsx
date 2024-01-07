import { Dispatch, ReactNode, SetStateAction, useEffect, useState } from "react";

const ChatBox = ({lines, animate}: {lines: {name: ReactNode, msg: ReactNode}[], animate: boolean}): JSX.Element => {
    let l = lines;

    if (animate) {
        const [lines_2, setLines] = useState([]);

        useEffect(() => {
            let index = 0;

            const addLine = () => {
                setLines((prevLines) => [...prevLines, lines[index]]);
                index = (index + 1) % lines.length;

                if (index === 0) {
                    setTimeout(() => {
                        setLines([]);
                    }, 2000);
                }

            };

            const interval = setInterval(addLine, 5000);

            return () => clearInterval(interval);
        }, []);

        l = lines_2;
    }
    
    return (<div className="h-48 min-w-80 flex flex-col-reverse bg-neutral-900 border-2 border-neutral-950 rounded-lg text-gray-50 text-sm [&>*:nth-child(even)]:bg-neutral-800 overflow-y-scroll">
        <div>
        {
            l.map(({name, msg}, index) => (
                <ChatLine name={name} message={msg} color="text-teal-500" key={index} />
            ))
        }
        </div>
    </div>);
};

const ChatLine = ({name, message, color}: {name: ReactNode, message: ReactNode, color: string}): JSX.Element => (
    <div className="flex flex-row p-1 border-b-2 border-neutral-700 min-w-80 h-10">
        <p className={"m-1 font-medium " + color}>{name + ": "}</p> {message}
    </div>
);

export default ChatBox;