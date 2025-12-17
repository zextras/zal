#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

OS=${1:-"ubuntu-jammy"}
declare VERSION
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Building package for version ${VERSION}"
cp target/zal.jar packages/
docker run -it --rm \
    --entrypoint=yap \
    -v "$(pwd)/artifacts/${OS}":/artifacts \
    -v "$(pwd)":/tmp/staging \
    "docker.io/m0rf30/yap-${OS}:1.44" \
    build "${OS}" /tmp/staging
