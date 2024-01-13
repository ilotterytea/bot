import { NextRequest, NextResponse } from "next/server";

export async function middleware(request: NextRequest): Promise<NextResponse> {
    const cookies = request.cookies;

    const token = cookies.get("token");
    const client_id = cookies.get("client_id");

    if (!token || !client_id) {
        return NextResponse.rewrite(new URL("/login", request.url));
    }

    let response = NextResponse.next();

    let r = await fetch(
        "https://api.twitch.tv/helix/users",
        {
            headers: {
                "Authorization": "Bearer " + token.value,
                "Client-Id": client_id.value
            }
        }
    );

    let j = await r.json();
    let user = j.data[0];

    // maybe it can be done better
    response.cookies.set("twitch_user_data", JSON.stringify(user));

    return response;
}

export const config = {
    matcher: ["/dashboard/:path*"]
}