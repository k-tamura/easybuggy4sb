#!/usr/bin/env python3

import os
import random
import string
import time

import requests
from playwright.sync_api import sync_playwright

BASE_URL = "https://keycloak:8443/auth"
CAPSOLVER_API_KEY = os.environ.get("CAPSOLVER_API_KEY", "CAP-047752D6421D-INVALID-KEY-FC156E1CAXXXA0794FAA4D3BCF39C5DC8C5CB6B6")
CAPSOLVER_API = "https://api.capsolver.com"

def solve_recaptcha_v2(website_url: str, website_key: str, timeout: int = 120) -> str:
    """Solve reCAPTCHA v2 using CapSolver and return the g-recaptcha-response token."""
    res = requests.post(f"{CAPSOLVER_API}/createTask", json={
        "clientKey": CAPSOLVER_API_KEY,
        "task": {
            "type": "ReCaptchaV2TaskProxyLess",
            "websiteURL": website_url,
            "websiteKey": website_key,
        },
    }, timeout=30).json()
    if res.get("errorId"):
        raise RuntimeError(f"createTask failed: {res}")
    task_id = res["taskId"]

    deadline = time.time() + timeout
    while time.time() < deadline:
        time.sleep(3)
        r = requests.post(f"{CAPSOLVER_API}/getTaskResult", json={
            "clientKey": CAPSOLVER_API_KEY,
            "taskId": task_id,
        }, timeout=30).json()
        if r.get("errorId"):
            raise RuntimeError(f"getTaskResult failed: {r}")
        if r.get("status") == "ready":
            return r["solution"]["gRecaptchaResponse"]
    raise TimeoutError("Retrieving the result from CapSolver timed out")

def solve_and_set_recaptcha(page):
    """Extract sitekey, solve reCAPTCHA v2, and inject the token into the form."""
    # 1. reCAPTCHA sitekey を取得
    site_key = page.get_attribute(".g-recaptcha", "data-sitekey")
    if not site_key:
        raise RuntimeError("reCAPTCHA site key not found (please check your realm settings)")

    # 2. CapSolver で reCAPTCHA を解く
    token = solve_recaptcha_v2(page.url, site_key)

    # 3. token を DOM に埋め込む
    page.evaluate(
        """(token) => {
            document.querySelectorAll('textarea[name="g-recaptcha-response"]').forEach(el => {
                el.style.display = 'block';
                el.value = token;
            });
        }""",
        token,
    )

def main():

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        processed_count = 0
        with open("usernames.txt", "r", encoding="utf-8") as f:
            for line in f:
                username = line.strip()
                # Ignore certificate errors due to a self-signed certificate
                context = browser.new_context(ignore_https_errors=True)
                page = context.new_page()

                # Access the login screen (navigate to the login screen via the account console)
                page.goto(f"{BASE_URL}/realms/master/account", wait_until="domcontentloaded")

                # Click the Register link
                page.click("#kc-registration a")
                page.wait_for_selector("form#kc-register-form")

                # Enter profile information
                page.fill("input[name=firstName]", username)
                page.fill("input[name=lastName]", username)
                page.fill("input[name=email]", username + "@gmail.com")
                page.fill("input[name=username]", username)
                page.fill("#password", "Pas2w0rd")
                page.fill("#password-confirm", "Pas2w0rd")

                # reCAPTCHA v2: Solve and inject token
                # solve_and_set_recaptcha(page)

                # Click the registration button
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
