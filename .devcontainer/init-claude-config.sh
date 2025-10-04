#!/bin/bash
set -e

# Initialize Claude MCP if not already configured
echo "Check mcp >> "
cat /home/node/.claude/mcp_settings.json 
echo "End mcp >> "
