#!/usr/bin/env bash

set -e

PAGE_URL="https://keycloak:8443/auth/realms/master/account"
OUT_DIR="tomcat/webapps/attacker-app/css"
mkdir -p "$OUT_DIR"

SEEN_FILE="$OUT_DIR/.seen"
touch "$SEEN_FILE"

normalize_url() {
  echo "$1" | sed 's/#.*$//'
}

resolve_url() {
  local base="$1"
  local url="$2"

  if echo "$url" | grep -Eiq '^https?://'; then
      echo "$url"
      return
  fi

  if echo "$url" | grep -Eiq '^//'; then
      echo "${base%%:*}:$url"
      return
  fi

  if echo "$url" | grep -Eiq '^/'; then
      echo "$(echo "$base" | sed -E 's#(https?://[^/]+).*#\1#')$url"
      return
  fi

  echo "$(echo "$base" | sed -E 's#(https?://.*/).*#\1#')$url"
}

extract_imports() {
  grep -Eo '@import[^;]*;' | \
    sed -E 's/@import//; s/url//g; s/["();]//g; s/^[[:space:]]+//; s/[[:space:]]+$//'
}

download_css() {
  local css_url
  css_url=$(normalize_url "$1")

  if grep -Fxq "$css_url" "$SEEN_FILE"; then
    return
  fi
  echo "$css_url" >> "$SEEN_FILE"

  local filename
  filename=$(basename "$css_url")
  if ! echo "$filename" | grep -Eq '\.css$'; then
      filename="${filename}.css"
  fi
  filename="$OUT_DIR/$filename"

  if ! curl -k -fsSL "$css_url" -o "$filename"; then
    return
  fi

  local base_dir
  base_dir="$css_url"

  imports=$(extract_imports < "$filename")
  for imp in $imports; do
      imp_url=$(resolve_url "$base_dir" "$imp")
      download_css "$imp_url"
  done
}

HTML=$(curl -k -fsSL "$PAGE_URL")

links=$(echo "$HTML" | grep -Eo '<link[^>]+rel="stylesheet"[^>]+>' | \
        grep -Eo 'href="[^"]+"' | sed 's/href="//; s/"$//')

style_imports=$(echo "$HTML" | sed -n '/<style/,/<\/style>/p' | extract_imports)

for css in $links; do
  resolved=$(resolve_url "$PAGE_URL" "$css")
  download_css "$resolved"
done

for imp in $style_imports; do
  resolved=$(resolve_url "$PAGE_URL" "$imp")
  download_css "$resolved"
done
