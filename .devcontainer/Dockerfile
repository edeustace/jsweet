FROM node:22 AS base-tools

ARG TZ
ENV TZ="$TZ"

ARG CLAUDE_CODE_VERSION=latest



# Install basic development tools and iptables/ipset
RUN apt-get update && apt-get install -y --no-install-recommends \
  less \
  git \
  procps \
  sudo \
  fzf \
  man-db \
  unzip \
  gnupg2 \
  gh \
  iptables \
  ipset \
  iproute2 \
  dnsutils \
  aggregate \
  jq \
  nano \
  vim 


# jdk - Install Temurin (Adoptium) OpenJDK 11
RUN apt-get update && apt-get install -y --no-install-recommends wget apt-transport-https && \
  wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
  echo "deb https://packages.adoptium.net/artifactory/deb bookworm main" | tee /etc/apt/sources.list.d/adoptium.list && \
  apt-get update && \
  apt-get install -y --no-install-recommends temurin-11-jdk && \
  apt-get clean && rm -rf /var/lib/apt/lists/*

# add pnpm
RUN npx corepack enable
RUN corepack enable pnpm



# Ensure default node user has access to /usr/local/share
RUN mkdir -p /usr/local/share/npm-global && \
  chown -R node:node /usr/local/share


ARG USERNAME=node

# Persist bash history.
RUN SNIPPET="export PROMPT_COMMAND='history -a' && export HISTFILE=/commandhistory/.bash_history" \
  && mkdir /commandhistory \
  && touch /commandhistory/.bash_history \
  && chown -R $USERNAME /commandhistory

# Set `DEVCONTAINER` environment variable to help with orientation
ENV DEVCONTAINER=true

# Create workspace and config directories and set permissions
RUN mkdir -p /workspace /home/node/.claude && \
  chown -R node:node /workspace /home/node/.claude

WORKDIR /workspace

ARG GIT_DELTA_VERSION=0.18.2
RUN ARCH=$(dpkg --print-architecture) && \
  wget "https://github.com/dandavison/delta/releases/download/${GIT_DELTA_VERSION}/git-delta_${GIT_DELTA_VERSION}_${ARCH}.deb" && \
  sudo dpkg -i "git-delta_${GIT_DELTA_VERSION}_${ARCH}.deb" && \
  rm "git-delta_${GIT_DELTA_VERSION}_${ARCH}.deb"


FROM base-tools AS user-env
# Set up non-root user
USER node

RUN curl https://mise.run | sh

ENV PATH=/home/${USERNAME}/.local/bin:~/.local/share/mise/shims:$PATH

RUN mise install maven
# We install jdk via apt - broken here.
# RUN mise install java@zulu-11.82.19.0 

# Install global packages
ENV NPM_CONFIG_PREFIX=/usr/local/share/npm-global
ENV PATH=$PATH:/usr/local/share/npm-global/bin

ENV SHELL=/bin/bash
ENV PNPM_HOME=/home/$USERNAME/.local/share/pnpm
ENV PATH=$PNPM_HOME:$PATH

# Set the default editor and visual
ENV EDITOR=vim
ENV VISUAL=vim

FROM user-env AS claude-mcp

# Install Claude
RUN npm install -g @anthropic-ai/claude-code@${CLAUDE_CODE_VERSION}

# Install Playwright MCP via npm
RUN pnpm install -g @playwright/mcp@latest

RUN claude mcp add playwright \
  --scope user \
  pnpx @playwright/mcp@latest


RUN echo 'eval "$(~/.local/bin/mise activate bash)"' >> ~/.bashrc

RUN echo 'mise trust' >> ~/.bashrc

# Copy and set up firewall script
COPY init-firewall.sh /usr/local/bin/
USER root
RUN chmod +x /usr/local/bin/init-firewall.sh && \
  echo "node ALL=(root) NOPASSWD: /usr/local/bin/init-firewall.sh" > /etc/sudoers.d/node-firewall && \
  chmod 0440 /etc/sudoers.d/node-firewall
USER node