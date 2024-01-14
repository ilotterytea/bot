const withMDX = require('@next/mdx')()
 
/** @type {import('next').NextConfig} */
const nextConfig = {
  // Configure `pageExtensions` to include MDX files
  pageExtensions: ['js', 'jsx', 'mdx', 'ts', 'tsx'],
  // Optionally, add any other Next.js config below
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "static-cdn.jtvnw.net"
      }
    ]
  },
  async redirects() {
    return [
      {
        source: "/",
        destination: "/under-construction",
        permanent: false
      }
    ]
  }
}
 
module.exports = withMDX(nextConfig)