#!/usr/bin/env python3

import os
import random
import string
import time

import requests
from playwright.sync_api import sync_playwright

BASE_URL = "https://keycloak:8443/auth"
CAPSOLVER_API_KEY = os.environ.get("CAPSOLVER_API_KEY", "")
CAPSOLVER_API = "https://api.capsolver.com"

def solve_recaptcha_v2(website_url: str, website_key: str, timeout: int = 120) -> str:
    """CapSolverでreCAPTCHA v2を解き、g-recaptcha-responseのトークンを返す"""
    res = requests.post(f"{CAPSOLVER_API}/createTask", json={
        "clientKey": CAPSOLVER_API_KEY,
        "task": {
            "type": "ReCaptchaV2TaskProxyLess",
            "websiteURL": website_url,
            "websiteKey": website_key,
        },
    }, timeout=30).json()
    if res.get("errorId"):
        raise RuntimeError(f"createTask失敗: {res}")
    task_id = res["taskId"]

    deadline = time.time() + timeout
    while time.time() < deadline:
        time.sleep(3)
        r = requests.post(f"{CAPSOLVER_API}/getTaskResult", json={
            "clientKey": CAPSOLVER_API_KEY,
            "taskId": task_id,
        }, timeout=30).json()
        if r.get("errorId"):
            raise RuntimeError(f"getTaskResult失敗: {r}")
        if r.get("status") == "ready":
            return r["solution"]["gRecaptchaResponse"]
    raise TimeoutError("CapSolverの結果取得がタイムアウトしました")

def main():

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        processed_count = 0
        with open("usernames.txt", "r", encoding="utf-8") as f:
            for line in f:
                username = line.strip()
                # 自己署名証明書のため証明書エラーを無視
                context = browser.new_context(ignore_https_errors=True)
                page = context.new_page()

                # ログイン画面へアクセス(アカウントコンソール経由でログイン画面に遷移)
                page.goto(f"{BASE_URL}/realms/master/account", wait_until="domcontentloaded")

                # Registerリンクをクリック
                page.click("#kc-registration a")
                page.wait_for_selector("form#kc-register-form")

                # プロファイル入力
                page.fill("input[name=firstName]", username)
                page.fill("input[name=lastName]", username)
                page.fill("input[name=email]", username + "@gmail.com")
                page.fill("input[name=username]", username)
                page.fill("#password", "Pas2w0rd")
                page.fill("#password-confirm", "Pas2w0rd")

                # reCAPTCHA v2: サイトキーを画面から取得し、CapSolverでトークンを取得
                # site_key = page.get_attribute(".g-recaptcha", "data-sitekey")
                # if not site_key:
                #     raise RuntimeError("reCAPTCHAのsitekeyが見つかりません(レルムの設定を確認してください)")
                # token = solve_recaptcha_v2(page.url, site_key)

                # トークンをフォームに設定(Keycloakはcallbackなしでフォーム送信時に値を読む)
                # page.evaluate(
                #     """(token) => {
                #         document.querySelectorAll('textarea[name="g-recaptcha-response"]').forEach(el => {
                #             el.style.display = 'block';
                #             el.value = token;
                #         });
                #     }""",
                #     token,
                # )

                # 登録ボタンをクリック
                page.click("form#kc-register-form [type=submit]")
                page.wait_for_load_state("domcontentloaded")

                locator = page.locator("#input-error-username")

                if locator.count() > 0:
                    msg = locator.inner_text()
                    if "Username already exists" in msg:
                        print(f"Username {username} already exists.")
                processed_count += 1

                if processed_count % 10 == 0:
                    print(f"{processed_count} user registrations processed.")

        print(f"Processing completed. Total: {processed_count} user registered.")
        browser.close()

if __name__ == "__main__":
    main()
