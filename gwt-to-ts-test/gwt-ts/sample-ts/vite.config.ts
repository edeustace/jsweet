import { defineConfig } from "vite";
import circularDependency from "vite-plugin-circular-dependency";
export default defineConfig({
  plugins: [
    // circularDependency({ outputFilePath: "./cdep.txt" }),
    // {
    //   name: "ignoreHMRCircularDependencies",
    //   hotUpdate({ modules }) {
    //     modules.forEach((m) => {
    //       m.importedModules = new Set();
    //       m.importers = new Set();
    //     });
    //     return modules;
    //   },
    // },
  ],
  define: {
    global: "globalThis",
    "process.env.NODE_ENV": '"production"',
  },
  resolve: {
    alias: {
      // Use browser-compatible jree replacement
      jree: "/Users/ed.eustace/dev/ghe.megaleo.com/ed-eustace/java2typescript/gwt-to-ts-test/gwt-ts/browser-jree.ts",
      // Provide browser-compatible alternatives for Node.js modules
      os: "os-browserify",
      path: "path-browserify",
      crypto: "crypto-browserify",
      stream: "stream-browserify",
      util: "util",
      events: "events",
      buffer: "buffer",
      vm: "vm-browserify",
      fs: "memfs", // Mock filesystem
      process: "process/browser",
    },
  },
  build: {
    rollupOptions: {
      external: [], // Don't externalize anything now
      output: {
        format: "es",
      },
      // Handle TypeScript interface resolution issues
      onwarn(warning, warn) {
        // Suppress "not exported" warnings for TypeScript interfaces
        if (
          warning.code === "UNRESOLVED_IMPORT" &&
          warning.message.includes("is not exported by")
        ) {
          return;
        }
        warn(warning);
      },
    },
  },
});
