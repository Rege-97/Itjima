import React from "react";
import { PaperProvider } from "react-native-paper";
import { registerTranslation } from "react-native-paper-dates";
import { AuthProvider } from "./src/contexts/AuthContext";
import AppNavigator from "./src/navigation/AppNavigator";

registerTranslation("ko", {
  save: "확인",
  selectSingle: "날짜 선택",
  selectMultiple: "여러 날짜 선택",
  selectRange: "기간 선택",
  notAccordingToDateFormat: (inputFormat: string) =>
    `날짜 형식은 ${inputFormat} 이어야 합니다`,
  mustBeHigherThan: (date: string) => `${date} 이후여야 합니다`,
  mustBeLowerThan: (date: string) => `${date} 이전이어야 합니다`,
  mustBeBetween: (startDate: string, endDate: string) =>
    `${startDate} ~ ${endDate} 사이여야 합니다`,
  dateIsDisabled: (date: string) => `${date}는 선택할 수 없습니다`,
  previous: "이전",
  next: "다음",
  typeInDate: "날짜를 입력해주세요",
  pickDateFromCalendar: "캘린더에서 날짜 선택",
  close: "닫기",
} as any);

export default function App() {
  return (
    <AuthProvider>
      <PaperProvider>
        <AppNavigator />
      </PaperProvider>
    </AuthProvider>
  );
}
