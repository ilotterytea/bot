import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import "@fortawesome/fontawesome-svg-core/styles.css";
import { CookiesProvider } from 'next-client-cookies/server';
import { Providers } from './providers';
import { NavigationBar } from '@/components/Navbar';
import Footer from '@/components/Footer';

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: {
    default: "ilotterytea's twitch bot",
    template: "%s | itb"
  },
  description: "a multi-utility twitch chat bot that brings a lot of functionality into your chat.",
  applicationName: "itb",
  
  authors: [{name: "ilotterytea", url: "https://ilotterytea.kz" }],
  creator: "ilotterytea",
  
  keywords: ["bot", "twitch bot", "twitch", "stream", "ilotterytea"]
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <CookiesProvider>
          <Providers>
            <div className="w-full min-h-[100vh] flex flex-col justify-center items-center bg-[url('/backgrounds/bg.png')] bg-stone-100 dark:bg-stone-950  bg-fixed">
              <NavigationBar />

              <div className="w-full grow min-h-[100vh] lg:w-[50%]">
                {children}
              </div>

              <Footer />
            </div>
          </Providers>
        </CookiesProvider>
      </body>
    </html>
  )
}
