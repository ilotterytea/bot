use actix_web::{HttpRequest, HttpResponse};
use include_dir::{include_dir, Dir};

const STATIC_FILES: Dir = include_dir!("$CARGO_MANIFEST_DIR/static");

pub async fn get_static_file(req: HttpRequest) -> HttpResponse {
    let path = req.match_info().query("filename");

    if let Some(file) = STATIC_FILES.get_file(&path) {
        let mime = mime_guess::from_path(&path).first_or_octet_stream();
        let body = file.contents();

        HttpResponse::Ok().content_type(mime.as_ref()).body(body)
    } else {
        HttpResponse::NotFound().finish()
    }
}
