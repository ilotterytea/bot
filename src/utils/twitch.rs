use eyre::Context;
use twitch_api::twitch_oauth2::{client::Client, AccessToken, UserToken};

pub async fn make_token(
    client: &impl Client,
    token: impl Into<AccessToken>,
) -> Result<UserToken, eyre::Report> {
    UserToken::from_token(client, token.into())
        .await
        .context("Couldn't get/make access token")
        .map_err(Into::into)
}

pub async fn get_access_token(
    client: &reqwest::Client,
    access_token: Option<String>,
    oauth_service_url: Option<String>,
    oauth2_service_pointer: Option<String>,
    oauth2_service_key: Option<String>,
) -> Result<UserToken, eyre::Report> {
    if let Some(access_token) = access_token {
        make_token(client, AccessToken::new(access_token)).await
    } else if let (Some(ref oauth_service_url), Some(ref pointer)) =
        (&oauth_service_url, &oauth2_service_pointer)
    {
        let mut request = client.get(oauth_service_url.as_str());
        if let Some(ref key) = oauth2_service_key {
            request = request.bearer_auth(key);
        }

        let request = request.build()?;

        match client.execute(request).await {
            Ok(response)
                if !(response.status().is_client_error())
                    || response.status().is_server_error() =>
            {
                let service_response: serde_json::Value = response
                    .json()
                    .await
                    .context("when transforming oauth sevice response to json")?;
                make_token(
                    client,
                    service_response
                        .pointer(pointer)
                        .ok_or_else(|| eyre::eyre!("couldn't get a field on {}", pointer))?
                        .as_str()
                        .ok_or_else(|| eyre::eyre!("token is not a string"))?
                        .to_string(),
                )
                .await
            }
            Ok(response_error) => {
                let status = response_error.status();
                let error = response_error.text().await?;
                eyre::bail!(
                    "oauth service returned error code: {} with body: {:?}",
                    status,
                    error
                );
            }
            Err(e) => Err(e)
                .wrap_err_with(|| eyre::eyre!("calling oauth service on {}", &oauth_service_url)),
        }
    } else {
        panic!("Neither oauth service url, oauth2 service pointer, nor access token was provided");
    }
}
