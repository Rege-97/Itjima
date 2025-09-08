import { privateApi } from "./core";

export const getMyAgreementsApi = (
  lastId?: number,
  role?: string,
  keyword?: string
) => {
  let url = "/agreements/summary?";
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  if (role) {
    url += `role=${role}&`;
  }
  if (keyword) {
    url += `keyword=${keyword}&`;
  }
  return privateApi.get(url);
};

export const getAgreementDetailApi = (agreementId: Number) => {
  return privateApi.get(`/agreements/${agreementId}/detail`);
};

export const agreementAccept = (agreementId: Number) => {
  return privateApi.put(`/agreements/${agreementId}/accept`);
};

export const agreementReject = (agreementId: Number) => {
  return privateApi.put(`/agreements/${agreementId}/reject`);
};

export const agreementCancel = (agreementId: Number) => {
  return privateApi.put(`/agreements/${agreementId}/cancel`);
};

export const agreementComplete = (agreementId: Number) => {
  return privateApi.put(`/agreements/${agreementId}/complete`);
};

export const agreementTransaction = (
  agreementId: Number,
  params: { amount: string }
) => {
  return privateApi.post(`/agreements/${agreementId}/transactions`, params);
};

export const agreementExtend = (
  agreementId: Number,
  params: { dueAt: string }
) => {
  return privateApi.put(`/agreements/${agreementId}/extend`, params);
};

export const getAgreementLogsApi = (agreementId: Number, lastId?: Number) => {
  let url = `/agreements/${agreementId}/logs?`;
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  console.log(url);
  return privateApi.get(url);
};

export const getAgreementTransactionsApi = (
  agreementId: Number,
  lastId?: Number
) => {
  let url = `/agreements/${agreementId}/transactions?`;
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  console.log(url);
  return privateApi.get(url);
};

export const confirmTransactionApi = (transactionId: number) => {
  return privateApi.put(`/transactions/${transactionId}/confirm`);
};

export const rejectTransactionApi = (transactionId: number) => {
  return privateApi.put(`/transactions/${transactionId}/reject`);
};
