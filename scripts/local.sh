#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

# Format code using Spotless
format_code() {
    echo -e "${CYAN}Checking code format...${NC}"
    if ! mvn spotless:check; then
        echo -e "${CYAN}Applying code format...${NC}"
        mvn spotless:apply || {
            echo -e "${RED}Formatting failed.${NC}"
            exit 1
        }
    else
        echo -e "${GREEN}Code format is up to date.${NC}"
    fi
}

# Build application
build_app() {
    format_code
    echo -e "${CYAN}Building application...${NC}"
    mvn clean install || {
        echo -e "${RED}Build failed.${NC}"
        exit 1
    }
    echo -e "${GREEN}Build successful.${NC}"
}

# Run tests
run_tests() {
    echo -e "${CYAN}Running tests...${NC}"
    mvn test || {
        echo -e "${RED}Tests failed.${NC}"
        exit 1
    }
    echo -e "${GREEN}Tests passed.${NC}"
}

# Run application locally
run_local() {
    build_app
    JAR_FILE=$(find target -maxdepth 1 -type f -name "scholar-ai-*.jar" | grep -vE "javadoc|original" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}JAR not found. Did you build it?${NC}"
        exit 1
    fi
    java -jar "$JAR_FILE" || {
        echo -e "${RED}App failed to start.${NC}"
        exit 1
    }
}

case "$1" in
"format") format_code ;;
"build") build_app ;;
"test") run_tests ;;
"run") run_local ;;
*)
    echo -e "${RED}Usage: $0 {format|build|test|run}${NC}"
    exit 1
    ;;
esac
