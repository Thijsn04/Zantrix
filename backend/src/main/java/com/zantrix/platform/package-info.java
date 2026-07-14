/**
 * Platform module: cross cutting foundation shared by every capability.
 *
 * <p>Holds security, and the system level web surface. Domain capabilities are
 * separate Spring Modulith modules that build on this foundation. See
 * {@code docs/architecture/backend.md}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Platform")
package com.zantrix.platform;
