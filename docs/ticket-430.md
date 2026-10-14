# Ticket 430: redundant descendant removals

REQ-430: When refreshing or hiding a previously sent advancement tree, the API
shall send only its highest sent ancestors as removal keys, because Minecraft
recursively removes their descendants. Independent trees and custom onUpdate
advancements shall still be removed; progress, rewards and display data shall not change.

Evidence: https://mclo.gs/9LAu8zZ contains 196 unknown-removal warnings at
15:53:38-39. UAA 2.8.1 bytecode and current main/dev dfbcbb4 both store all
sent keys and remove them together on refresh. This matches the reported failure
mechanism but does not establish live acceptance or rule out another packet sender.

SPEAR: spec recorded. Prove: five regression cases against a faithful extraction
of the previous all-keys selection yielded three failures (parent/descendant,
independent custom trees, and an unsent intermediate ancestor). Engine: store
only highest sent ancestor keys; preserve the complete send/progress packet.
Arch: package-private generic helper, no public API/NMS protocol/persistence change;
the same per-player snapshot feeds refresh, hide and disposal, including their
existing rollback on packet-construction failure. Refine: Java 25 Maven
`-B -ntp -pl Common -am -Dmaven.javadoc.skip=true clean verify` passed all
27 tests (6 NMS Common, 21 Common, including 5 new cases); Checkstyle and
`git diff --check` passed. Javadoc generation was explicitly skipped.
No local EARS validator or state helper exists in this upstream project.
Use this manual evidence record. The full multi-version distribution was not built.
Boundary: user authorized the FainNeito fork and review PR on October 14.
Hosted CI, reviewed source, full plugin packaging, monorepo ownership/pinning and
Paper 26.2/26.3 client acceptance remain pending. A Common module JAR is NOT
an installable server plugin and must not be uploaded as one.
No server uploads, activation, database changes or warning suppression.
