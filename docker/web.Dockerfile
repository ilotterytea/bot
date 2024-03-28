ARG NODE_TAG=18

FROM node:$NODE_TAG AS builder
  WORKDIR /tmp/ilotterytea/bot/web
  COPY web .
  COPY .env .env

  RUN npm install
  RUN npm run build

  EXPOSE 3000
