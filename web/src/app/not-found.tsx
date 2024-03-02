import { Metadata } from "next";
import Image from "next/image";

export const metadata: Metadata = {
    title: "not found"
};

export default function NotFound() {
    const images = [
        "/404/1.gif"
    ];

    return (
        <div className="w-full min-h-[calc(100vh-120px)] flex flex-col justify-center items-center space-y-4">
            <Image
                src={images[Math.floor(Math.random() * images.length)]}
                width={256}
                height={256}
                alt=""
            />
            <h1 className="text-6xl font-anta font-semibold [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">404</h1>
            <p className="text-xl [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">the requested page existed before or never existed at all.</p>
        </div>
    );
}