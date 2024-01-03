import Image from "next/image";

const Navbar = () : JSX.Element => (
    <div className="flex justify-between items-center h-9 bg-gray-100 border-solid border-slate-300 border-b-2 font-mono text-sm tracking-wider">
        {
        // Logo
        }
        <div className="flex items-center h-full">
            <a href="/" className="flex items-center mx-1">
            <div className="w-5 mr-1">
                <Image
                    src="/tea.png"
                    width={0}
                    height={0}
                    style={{width: '100%', height: 'auto'}}
                    alt="Logo"
                />
            </div>
            <h1 className="font-mono text-teal-500 font-semibold">
                iLotterytea's Twitch bot
            </h1>
            </a>
        </div>
        {
        // Buttons
        }
        <div className="flex items-center flex-row h-full">
            <a href="/cmd" className="flex items-center border-slate-300 border-x-2 px-4 h-full">
                Commands
            </a>
            <a href="/login" className="flex items-center border-slate-300 border-x-2 px-4 h-full">
                Log in...
            </a>
            <form action={"/search"} style={{height: '100%', width: 'auto'}}>
                <input name={"query"} placeholder="Search user..." style={{height: '100%', width: 'auto'}}/>
            </form>
        </div>
    </div>
);

export default Navbar;