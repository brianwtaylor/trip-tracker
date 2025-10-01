#!/bin/bash

################################################################################
# Trip Tracker - Automated Setup Script
# This script will help you set up your development environment
################################################################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

################################################################################
# Helper Functions
################################################################################

print_header() {
    echo ""
    echo -e "${CYAN}=================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}=================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

ask_yes_no() {
    while true; do
        read -p "$1 (y/n): " yn
        case $yn in
            [Yy]* ) return 0;;
            [Nn]* ) return 1;;
            * ) echo "Please answer yes (y) or no (n).";;
        esac
    done
}

################################################################################
# Check Functions
################################################################################

check_java() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -eq 17 ]; then
            return 0
        fi
    fi
    return 1
}

check_homebrew() {
    if command -v brew &> /dev/null; then
        return 0
    fi
    return 1
}

check_android_studio() {
    if [ -d "/Applications/Android Studio.app" ]; then
        return 0
    fi
    return 1
}

check_android_sdk() {
    if [ -d "$HOME/Library/Android/sdk" ]; then
        return 0
    fi
    return 1
}

check_maps_api_key() {
    if [ -f "$SCRIPT_DIR/local.properties" ]; then
        if grep -q "MAPS_API_KEY=AIza" "$SCRIPT_DIR/local.properties"; then
            return 0
        fi
    fi
    return 1
}

################################################################################
# Installation Functions
################################################################################

install_homebrew() {
    print_header "Installing Homebrew"
    print_info "Homebrew is a package manager for macOS"
    
    if ask_yes_no "Install Homebrew now?"; then
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        
        # Add Homebrew to PATH for Apple Silicon Macs
        if [ -d "/opt/homebrew/bin" ]; then
            echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
            eval "$(/opt/homebrew/bin/brew shellenv)"
        fi
        
        if check_homebrew; then
            print_success "Homebrew installed successfully!"
            return 0
        else
            print_error "Homebrew installation failed"
            return 1
        fi
    else
        print_warning "Skipping Homebrew installation"
        return 1
    fi
}

install_java() {
    print_header "Installing Java JDK 17"
    print_info "JDK 17 is required to build Android apps"
    
    if ask_yes_no "Install JDK 17 via Homebrew?"; then
        print_info "Installing OpenJDK 17..."
        brew install openjdk@17
        
        print_info "Linking JDK to system..."
        sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
        
        # Add to PATH
        echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zprofile
        export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
        
        if check_java; then
            JAVA_VER=$(java -version 2>&1 | head -n 1)
            print_success "Java installed successfully: $JAVA_VER"
            return 0
        else
            print_error "Java installation failed"
            return 1
        fi
    else
        print_warning "Skipping Java installation"
        print_info "Manual download: https://adoptium.net/temurin/releases/?version=17"
        return 1
    fi
}

setup_maps_api_key() {
    print_header "Google Maps API Key Setup"
    
    echo "You need a Google Maps API key to run this app."
    echo ""
    echo "Steps to get an API key:"
    echo "1. Go to: https://console.cloud.google.com/"
    echo "2. Create a new project (or select existing)"
    echo "3. Enable 'Maps SDK for Android'"
    echo "4. Go to Credentials → Create Credentials → API Key"
    echo "5. Copy the API key"
    echo ""
    
    if ask_yes_no "Do you have a Google Maps API key ready?"; then
        read -p "Enter your Maps API key: " api_key
        
        # Update local.properties
        if [ -f "$SCRIPT_DIR/local.properties" ]; then
            # Replace existing key or add new one
            if grep -q "MAPS_API_KEY=" "$SCRIPT_DIR/local.properties"; then
                sed -i.bak "s/MAPS_API_KEY=.*/MAPS_API_KEY=$api_key/" "$SCRIPT_DIR/local.properties"
            else
                echo "MAPS_API_KEY=$api_key" >> "$SCRIPT_DIR/local.properties"
            fi
            print_success "API key saved to local.properties"
        else
            print_error "local.properties not found"
            return 1
        fi
    else
        print_warning "You'll need to add the API key later to local.properties"
        print_info "Edit: $SCRIPT_DIR/local.properties"
        print_info "Add line: MAPS_API_KEY=your_key_here"
    fi
}

################################################################################
# Main Setup Flow
################################################################################

print_header "Trip Tracker - Automated Setup"
echo "This script will help you set up your development environment."
echo ""
echo "It will install/configure:"
echo "  • Homebrew (if needed)"
echo "  • Java JDK 17"
echo "  • Google Maps API key"
echo ""
echo "Note: Android Studio must be installed manually"
echo "      Download from: https://developer.android.com/studio"
echo ""

if ! ask_yes_no "Continue with automated setup?"; then
    print_warning "Setup cancelled"
    exit 0
fi

################################################################################
# Step 1: Check Current Status
################################################################################

print_header "Step 1: System Check"

echo "Checking prerequisites..."
echo ""

# Check Homebrew
if check_homebrew; then
    print_success "Homebrew: Installed"
    HAS_BREW=true
else
    print_warning "Homebrew: Not installed"
    HAS_BREW=false
fi

# Check Java
if check_java; then
    JAVA_VER=$(java -version 2>&1 | head -n 1)
    print_success "Java JDK 17: Installed ($JAVA_VER)"
    HAS_JAVA=true
else
    print_warning "Java JDK 17: Not installed"
    HAS_JAVA=false
fi

# Check Android Studio
if check_android_studio; then
    print_success "Android Studio: Installed"
    HAS_AS=true
else
    print_warning "Android Studio: Not found"
    HAS_AS=false
fi

# Check Android SDK
if check_android_sdk; then
    print_success "Android SDK: Found at ~/Library/Android/sdk"
    HAS_SDK=true
else
    print_warning "Android SDK: Not found"
    HAS_SDK=false
fi

# Check Maps API Key
if check_maps_api_key; then
    print_success "Maps API Key: Configured"
    HAS_KEY=true
else
    print_warning "Maps API Key: Not configured"
    HAS_KEY=false
fi

echo ""

################################################################################
# Step 2: Install Missing Components
################################################################################

# Install Homebrew if needed
if [ "$HAS_BREW" = false ]; then
    if ! install_homebrew; then
        print_error "Cannot continue without Homebrew"
        exit 1
    fi
    HAS_BREW=true
fi

# Install Java if needed
if [ "$HAS_JAVA" = false ]; then
    if ! install_java; then
        print_error "Cannot continue without Java JDK 17"
        exit 1
    fi
    HAS_JAVA=true
fi

################################################################################
# Step 3: Android Studio Check
################################################################################

print_header "Step 3: Android Studio"

if [ "$HAS_AS" = false ]; then
    print_warning "Android Studio is not installed"
    echo ""
    echo "Android Studio must be installed manually:"
    echo "1. Download from: https://developer.android.com/studio"
    echo "2. Open the .dmg file"
    echo "3. Drag Android Studio to Applications"
    echo "4. Launch and complete the setup wizard"
    echo "5. Select 'Standard' installation"
    echo ""
    
    if ask_yes_no "Open download page in browser?"; then
        open "https://developer.android.com/studio"
    fi
    
    echo ""
    print_info "After installing Android Studio, run this script again or continue manually"
else
    print_success "Android Studio is already installed"
fi

################################################################################
# Step 4: Maps API Key
################################################################################

if [ "$HAS_KEY" = false ]; then
    setup_maps_api_key
fi

################################################################################
# Step 5: Project Validation
################################################################################

print_header "Step 5: Project Validation"

if [ -f "$SCRIPT_DIR/validate.sh" ]; then
    print_info "Running project validation..."
    echo ""
    bash "$SCRIPT_DIR/validate.sh"
else
    print_warning "Validation script not found"
fi

################################################################################
# Step 6: Summary and Next Steps
################################################################################

print_header "Setup Summary"

echo "Installation Status:"
echo ""
[ "$HAS_BREW" = true ] && print_success "Homebrew" || print_error "Homebrew"
[ "$HAS_JAVA" = true ] && print_success "Java JDK 17" || print_error "Java JDK 17"
[ "$HAS_AS" = true ] && print_success "Android Studio" || print_warning "Android Studio (manual install required)"
[ "$HAS_SDK" = true ] && print_success "Android SDK" || print_warning "Android SDK (install via Android Studio)"
[ "$HAS_KEY" = true ] && print_success "Maps API Key" || print_warning "Maps API Key (needs configuration)"

echo ""
print_header "Next Steps"

if [ "$HAS_JAVA" = true ] && [ "$HAS_AS" = true ] && [ "$HAS_KEY" = true ]; then
    print_success "All prerequisites met! You're ready to build."
    echo ""
    echo "To open the project:"
    echo "  1. Launch Android Studio"
    echo "  2. Click 'Open'"
    echo "  3. Select: $SCRIPT_DIR"
    echo "  4. Wait for Gradle sync"
    echo "  5. Click Run ▶️"
    echo ""
    echo "Or run:"
    echo "  open -a 'Android Studio' '$SCRIPT_DIR'"
    echo ""
    
    if ask_yes_no "Open project in Android Studio now?"; then
        open -a "Android Studio" "$SCRIPT_DIR"
        print_success "Android Studio launching..."
    fi
else
    print_warning "Some prerequisites are missing"
    echo ""
    echo "What's missing:"
    [ "$HAS_JAVA" = false ] && echo "  • Java JDK 17 - Run this script again to install"
    [ "$HAS_AS" = false ] && echo "  • Android Studio - Download: https://developer.android.com/studio"
    [ "$HAS_KEY" = false ] && echo "  • Maps API Key - Get from: https://console.cloud.google.com/"
    echo ""
    echo "After installing missing items, run this script again:"
    echo "  cd $SCRIPT_DIR"
    echo "  ./setup.sh"
fi

echo ""
print_info "For detailed instructions, see:"
echo "  • README_FIRST.md"
echo "  • SETUP_PREREQUISITES.md"
echo "  • OPEN_IN_ANDROID_STUDIO.md"

echo ""
print_header "Setup Complete!"

exit 0
