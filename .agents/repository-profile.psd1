@{
    Name = "YSM-Equipment_Render-Patch"
    SharedPaths = @(
        "AGENTS.md"
        ".agents"
        ".gitignore"
    )
    VersionPaths = @(
        "src"
        "gradle"
        "gradlew"
        "gradlew.bat"
    )
    MixedPaths = @(
        "README.md"
        "README_ja.md"
        "build.gradle"
        "settings.gradle"
        "gradle.properties"
    )
    ForbiddenTrackedPatterns = @(
        "(^|/)local-ysm/"
        "(^|/)ysm-analysis/"
        "(^|/)test-fixtures/private/"
        "(^|/)build/reports/"
        "(^|/)(?:decompile[d]?|private-reports?|runtime-names?|whole-jar-graphs?)/"
        "(^|/)(?:registry-report|name-report|whole-jar-graph)(?:[-_.].*)?\.(?:json|txt|csv|tsv|dot|graphml)$"
        "^(?!gradle/wrapper/gradle-wrapper\.jar$).+\.jar$"
        "\.(?:dll|so|dylib)$"
    )
    PropagationSiblingRepositories = @(
        "YSM-Mapping-API"
    )
    RepositoryVerifier = ".agents/skills/maintain-ysm-equipment-integration/scripts/verify-mapping-integration.ps1"
}
