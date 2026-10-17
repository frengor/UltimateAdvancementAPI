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

Release verification follow-up, 17 October 2026: current parent main/dev and fork
main remain dfbcbb4c. Added a scoped Common/NMS Common Actions job without server
credentials. Full canonical `mvn -B -ntp -Dmaven.javadoc.skip=true clean verify`
was attempted and stops at missing provided `org.spigotmc:spigot:1.15.2-R0.1-SNAPSHOT`.
The full NMS matrix requires BuildTools-generated server inputs absent locally;
no NMS module was removed or distribution artifact assembled from loose patches.
Separate upstream PR122 restores the owner's historical stale-player loading
guard in source. A local integration of both changes passes 36 Common/NMS Common
tests, zero failures/errors/skips, Java 25, with Javadocs skipped. This integration
is not an installable server plugin, upstream merge, or client acceptance.

Final release-build refinement, 17 October 2026: the combined fork candidate
1ef44837 now passes the complete canonical `mvn -B -ntp clean verify`, including
Javadocs and all 24 native adapters. All 41 tests pass, zero failures/errors/skips.
Both installable variants retain all 406 official 2.8.2 plugin classes plus the
package-private removal selector. The loading guard is present in packaged
bytecode, and all 44 renderer tests pass against the rebuilt API. Existing 2.8.1
public API signatures are preserved. Public CI was refined to build the API
library with tests and Javadocs; it does not claim to build the server plugin.
The required 1.21.11-R0.1 input uses official BuildTools revision 4583 because
the current alias produces R0.2. No dependency was relabelled or module removed.
The source builds are separate from upstream merge and native client acceptance.
