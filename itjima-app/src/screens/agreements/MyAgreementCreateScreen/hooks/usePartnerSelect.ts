import { useEffect, useMemo, useState } from "react";
import * as Contacts from "expo-contacts";
import { recentPartnersApi, searchUserByPhoneApi } from "../../../../api/users";
import { Alert } from "react-native";

export type User = { id: number; name: string; phone: string };

export const normalizePhone = (raw: string) => {
  if (!raw) return "";
  let phone = raw.replace(/[^0-9+]/g, "");
  if (phone.startsWith("+82")) phone = "0" + phone.slice(3);
  return phone;
};

export function usePartnerSelect() {
  const [searchPhone, setSearchPhone] = useState("");
  const [isSearching, setIsSearching] = useState(false);
  const [searchResult, setSearchResult] = useState<User | null>(null);

  const [contacts, setContacts] = useState<any[]>([]);
  const [contactSearch, setContactSearch] = useState("");
  const [contactsLoaded, setContactsLoaded] = useState(false);

  const [partners, setPartners] = useState<User[]>([]);
  const [isLoadingPartners, setIsLoadingPartners] = useState(false);
  const [hasNextPartners, setHasNextPartners] = useState(true);
  const [lastPartnerId, setLastPartnerId] = useState<number | null>(null);

  const [isSearchModalVisible, setIsSearchModalVisible] = useState(false);
  const [isContactsVisible, setIsContactsVisible] = useState(false);
  const [isRecentVisible, setIsRecentVisible] = useState(false);

  const handleSearch = async (phone?: string) => {
    const target = phone || searchPhone;
    if (!target) return;
    setIsSearching(true);
    try {
      const res = await searchUserByPhoneApi(normalizePhone(target));
      const user = res?.data?.data as User | undefined;
      setSearchResult(user ?? null);
      return user;
    } catch {
      setSearchResult(null);
      Alert.alert("가입되지 않은 사용자입니다.");
    } finally {
      setIsSearching(false);
    }
  };

  const loadContacts = async () => {
    const { status } = await Contacts.requestPermissionsAsync();
    if (status !== "granted") return;
    const { data } = await Contacts.getContactsAsync({
      fields: [Contacts.Fields.PhoneNumbers],
    });
    setContacts(data);
    setContactsLoaded(true);
  };

  const filteredContacts = useMemo(
    () =>
      contacts.filter((c) =>
        (c.name || "").toLowerCase().includes(contactSearch.toLowerCase())
      ),
    [contacts, contactSearch]
  );

  const loadPartners = async (reset?: boolean) => {
    if (isLoadingPartners) return;
    if (!hasNextPartners && !reset) return;

    setIsLoadingPartners(true);
    try {
      const cursor = reset ? undefined : lastPartnerId ?? undefined;
      const res = await recentPartnersApi(cursor);
      const payload = res.data?.data;

      setPartners((prev) =>
        reset ? payload?.items || [] : [...prev, ...(payload?.items || [])]
      );
      setHasNextPartners(!!payload?.hasNext);
      setLastPartnerId(payload?.lastId ?? null);
    } finally {
      setIsLoadingPartners(false);
    }
  };

  useEffect(() => {
    loadPartners();
  }, []);

  return {
    searchPhone,
    isSearching,
    searchResult,
    contacts,
    contactSearch,
    contactsLoaded,
    partners,
    isLoadingPartners,
    hasNextPartners,
    lastPartnerId,
    isSearchModalVisible,
    isContactsVisible,
    isRecentVisible,
    setSearchPhone,
    setSearchResult,
    setContactSearch,
    setIsSearchModalVisible,
    setIsContactsVisible,
    setIsRecentVisible,
    setPartners,
    setLastPartnerId,
    setHasNextPartners,
    handleSearch,
    loadContacts,
    loadPartners,
    filteredContacts,
    normalizePhone,
  };
}
