# Security Policy

Zantrix is an Electronic Health Record and handles highly sensitive data. We take security seriously and appreciate the work of security researchers in keeping the project and its users safe.

## Reporting a vulnerability

Please report security vulnerabilities privately. Do not open a public issue, pull request, or discussion for a suspected vulnerability, because that would disclose it before a fix is available.

To report a vulnerability, use GitHub's private vulnerability reporting for this repository through the Security tab, or contact the maintainer privately through the contact details on the maintainer's GitHub profile.

Please include:

- A description of the issue and its potential impact.
- Steps to reproduce, or a proof of concept, if you have one.
- The affected version, commit, or configuration.

## What to expect

- We will acknowledge your report as soon as we reasonably can.
- We will investigate, keep you informed of progress, and work on a fix.
- We will credit you for the discovery once a fix is released, unless you prefer to remain anonymous.

## Scope

Zantrix has a beta Milestone 1 clinical core but is not production ready or certified. Reports covering authentication, authorization, consent, emergency access, audit integrity, FHIR isolation, terminology validation, or clinical workflow safety are especially valuable. The exact implemented boundary is tracked in the [roadmap](docs/roadmap.md).

## Responsible disclosure

Please give us a reasonable opportunity to address an issue before any public disclosure. We are committed to working with you in good faith and to resolving valid issues promptly.

## Handling of test data

Do not use real patient data when testing Zantrix. The development environment ships with clearly non production credentials and synthetic accounts, for local use only. Never load real protected health information into a development or demonstration instance.
