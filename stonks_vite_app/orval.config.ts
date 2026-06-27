import { defineConfig } from "orval"

export default defineConfig({
  stonks: {
    input: {
      target: "../stonks_java/src/main/resources/openapi.yaml",
    },
    output: {
      mode: "tags-split",
      target: "src/__generated__/api/endpoints.ts",
      schemas: "src/__generated__/api/types",
      client: "react-query",
      mock: false,
      override: {
        mutator: {
          path: "src/api/mutator.ts",
          name: "customFetch",
        },
      },
    },
  },
})
