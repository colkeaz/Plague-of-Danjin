#!/usr/bin/env bash

# =============================================================================
# Plague of Danjin - Automated Setup Script (Linux/macOS)
# =============================================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo -e "${BLUE}===================================${NC}"
echo -e "${BLUE}  Plague of Danjin - Setup${NC}"
echo -e "${BLUE}===================================${NC}"
echo ""

# --- Detect OS ---
OS="unknown"
DISTRO="unknown"

if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
    echo -e "${GREEN}[OS]${NC} macOS detected"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO="$ID"
    fi
    if [[ "$DISTRO" == "ubuntu" || "$DISTRO" == "debian" || "$DISTRO" == "pop" || "$DISTRO" == "mint" ]]; then
        echo -e "${GREEN}[OS]${NC} Linux (Debian/Ubuntu family) detected"
    elif [[ "$DISTRO" == "fedora" || "$DISTRO" == "rhel" || "$DISTRO" == "centos" || "$DISTRO" == "rocky" || "$DISTRO" == "alma" ]]; then
        echo -e "${GREEN}[OS]${NC} Linux (Fedora/RHEL family) detected"
    else
        echo -e "${GREEN}[OS]${NC} Linux detected (distro: ${DISTRO})"
    fi
else
    echo -e "${YELLOW}[WARN]${NC} Unrecognized OS: $OSTYPE"
    echo "This script supports macOS and Linux."
fi

echo ""

# --- Check Java ---
JAVA_OK=false
JAVA_VERSION=""

check_java_version() {
    if command -v java &> /dev/null; then
        # Parse version from java -version output
        JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -1)
        JAVA_VERSION=$(echo "$JAVA_VERSION_OUTPUT" | sed -n 's/.*version "\([0-9]*\).*/\1/p')

        if [ -z "$JAVA_VERSION" ]; then
            # Try alternate parsing for versions like "17.0.1"
            JAVA_VERSION=$(echo "$JAVA_VERSION_OUTPUT" | grep -oP '"\K[0-9]+' | head -1)
        fi

        if [ -n "$JAVA_VERSION" ] && [ "$JAVA_VERSION" -ge 17 ] 2>/dev/null; then
            echo -e "${GREEN}[OK]${NC} Java ${JAVA_VERSION} detected. OK!"
            JAVA_OK=true
        elif [ -n "$JAVA_VERSION" ]; then
            echo -e "${YELLOW}[WARN]${NC} Java ${JAVA_VERSION} detected, but Java 17+ is required."
        else
            echo -e "${YELLOW}[WARN]${NC} Java found but could not determine version."
        fi
    else
        echo -e "${RED}[ERROR]${NC} Java is not installed or not in PATH."
    fi
}

check_java_version

# --- Install Java if needed ---
if [ "$JAVA_OK" = false ]; then
    echo ""

    if [[ "$OS" == "macos" ]]; then
        if command -v brew &> /dev/null; then
            echo -e "${BLUE}[INFO]${NC} Homebrew detected. Can install Java 17 via: brew install openjdk@17"
            echo ""
            read -p "Would you like to install Java 17 now? (y/n) " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo -e "${BLUE}[INSTALL]${NC} Running: brew install openjdk@17"
                brew install openjdk@17
                # Symlink for system Java wrappers
                echo -e "${BLUE}[INFO]${NC} You may need to run:"
                echo "  sudo ln -sfn \$(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk"
                echo ""
                # Re-check
                check_java_version
            else
                echo ""
                echo -e "${YELLOW}[INFO]${NC} Skipping automatic installation."
                echo -e "${YELLOW}[INFO]${NC} Download Java 17 manually from: https://adoptium.net/temurin/releases/"
                exit 1
            fi
        else
            echo -e "${YELLOW}[WARN]${NC} Homebrew not found. Cannot auto-install."
            echo -e "${YELLOW}[INFO]${NC} Install Homebrew first: https://brew.sh"
            echo -e "${YELLOW}[INFO]${NC} Or download Java 17 manually from: https://adoptium.net/temurin/releases/"
            exit 1
        fi
    elif [[ "$OS" == "linux" ]]; then
        if [[ "$DISTRO" == "ubuntu" || "$DISTRO" == "debian" || "$DISTRO" == "pop" || "$DISTRO" == "mint" ]]; then
            echo -e "${BLUE}[INFO]${NC} Can install Java 17 via: sudo apt install openjdk-17-jdk"
            echo ""
            read -p "Would you like to install Java 17 now? (y/n) " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo -e "${BLUE}[INSTALL]${NC} Running: sudo apt update && sudo apt install -y openjdk-17-jdk"
                sudo apt update && sudo apt install -y openjdk-17-jdk
                check_java_version
            else
                echo ""
                echo -e "${YELLOW}[INFO]${NC} Skipping automatic installation."
                echo -e "${YELLOW}[INFO]${NC} Download Java 17 manually from: https://adoptium.net/temurin/releases/"
                exit 1
            fi
        elif [[ "$DISTRO" == "fedora" || "$DISTRO" == "rhel" || "$DISTRO" == "centos" || "$DISTRO" == "rocky" || "$DISTRO" == "alma" ]]; then
            echo -e "${BLUE}[INFO]${NC} Can install Java 17 via: sudo dnf install java-17-openjdk-devel"
            echo ""
            read -p "Would you like to install Java 17 now? (y/n) " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo -e "${BLUE}[INSTALL]${NC} Running: sudo dnf install -y java-17-openjdk-devel"
                sudo dnf install -y java-17-openjdk-devel
                check_java_version
            else
                echo ""
                echo -e "${YELLOW}[INFO]${NC} Skipping automatic installation."
                echo -e "${YELLOW}[INFO]${NC} Download Java 17 manually from: https://adoptium.net/temurin/releases/"
                exit 1
            fi
        else
            echo -e "${YELLOW}[WARN]${NC} Unsupported Linux distribution for auto-install."
            echo -e "${YELLOW}[INFO]${NC} Download Java 17 manually from: https://adoptium.net/temurin/releases/"
            exit 1
        fi
    else
        echo -e "${YELLOW}[INFO]${NC} Download Java 17 manually from: https://adoptium.net/temurin/releases/"
        exit 1
    fi
fi

echo ""

# --- Check JAVA_HOME ---
if [ -z "$JAVA_HOME" ]; then
    echo -e "${YELLOW}[WARN]${NC} JAVA_HOME is not set."
    # Try to detect it
    if command -v java &> /dev/null; then
        DETECTED_JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java) 2>/dev/null || echo $(which java))))
        echo -e "${YELLOW}[HINT]${NC} Try adding this to your shell profile:"
        echo "  export JAVA_HOME=\"$DETECTED_JAVA_HOME\""
    fi
    echo ""
else
    echo -e "${GREEN}[OK]${NC} JAVA_HOME is set to: $JAVA_HOME"
fi

# --- Check Gradle wrapper ---
echo ""
if [ -f "./gradlew" ]; then
    if [ ! -x "./gradlew" ]; then
        echo -e "${YELLOW}[FIX]${NC} Making gradlew executable..."
        chmod +x ./gradlew
    fi
    echo -e "${GREEN}[OK]${NC} Gradle wrapper found and executable."
else
    echo -e "${RED}[ERROR]${NC} Gradle wrapper (gradlew) not found!"
    echo "  Make sure you're running this script from the project root."
    exit 1
fi

# --- Verification build ---
echo ""
echo -e "${BLUE}[BUILD]${NC} Running verification build: ./gradlew classes"
echo ""

if ./gradlew classes; then
    echo ""
    echo -e "${GREEN}===================================${NC}"
    echo -e "${GREEN}  Setup complete!${NC}"
    echo -e "${GREEN}===================================${NC}"
    echo ""
    echo -e "  Run the game with: ${BLUE}./gradlew run${NC}"
    echo ""
else
    echo ""
    echo -e "${RED}===================================${NC}"
    echo -e "${RED}  Build failed!${NC}"
    echo -e "${RED}===================================${NC}"
    echo ""
    echo -e "${RED}[ERROR]${NC} The build did not succeed."
    echo "  Please check that Java 17+ is correctly installed and JAVA_HOME is set."
    echo "  Java version: $(java -version 2>&1 | head -1)"
    exit 1
fi
