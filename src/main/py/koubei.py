# encoding: utf8

import json

BIG_CATEGORIES = json.loads(
    '{"1":"教育培训","13":"购物败家","18":"房产装修","25":"工作职场","31":"旅游出行","46":"社交网络","55":"财富投资","65":"健康医疗","72":"游戏网站","89":"科技数码","97":"文学艺术","108":"生活助手","119":"影视音乐","130":"商务服务","140":"行业网站","152":"综合资讯"}')

BIG_URL = 'http://koubei.baidu.com/p/gettradetaginfoajax?tradeid=%s'

PER_PAGE = 18

import requests

import os, sys

DIR = os.path.abspath(os.path.dirname(__file__))

TMP_FILE = '%s/koubei_list.json' % DIR
KOUBEI_list = '%s/koubei_all_list.json' % DIR

HEADERS = {'Referer': 'http://koubei.baidu.com/rank',
           'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36'}


def download_list():
    out = open(TMP_FILE, 'w')

    for id, name in BIG_CATEGORIES.items():
        url = BIG_URL % id
        resp = requests.get(url, headers=HEADERS)
        # print resp.text
        o = {'id': id, 'name': name, 'resp': resp.text}
        out.write(json.dumps(o) + '\n')
        # break

        print id, name, url


def gen_download_urls():
    out = open('/tmp/urls', 'w')

    F = 'http://koubei.baidu.com/p/gettradesitesajax?tradeid=%s&childid=%s&page=%s&_=1420952333272'
    categories = {}

    for line in open(TMP_FILE):
        data = json.loads(line)
        tradeid = data['id']
        # d = {}
        # d['tradeid'] = tradeid
        # d['name'] = data['name']
        # d['cats'] = {}

        cates = {}

        for cate in json.loads(data['resp'])['data']:
            cid = cate['cid']
            if cid == 0:
                print data['name']
            else:
                cname, ccount = cate['cname'], cate['ccount']
                if ccount > 0:
                    for page in range(ccount / 18 + 1):
                        # print cname, ccount, page + 1
                        url = F % (tradeid, cid, page + 1)

                        out.write(url + "\n")
                        # d['cats'].append((cid, cname))
                        cates[str(cid)] = cname

        categories[str(tradeid)] = {'name': data['name'], 'cats': cates}

    return categories

    # resp = requests.get(url, headers=HEADERS)

    # o = {'url': url, 'cate1': data['name'], 'cname': cname, 'resp': resp.text}

    # out.write(json.dumps(o) + '\n')

    # print url, len(resp.text)


    # def pase_result():
    # for line in open('/private/tmp/datas'):


from bs4 import BeautifulSoup

import re


def parse_result():
    categories = gen_download_urls()

    out = open('/tmp/baidu_koubei_r', 'w')

    jump = open('/tmp/baidu_koubei_jump', 'w')

    def write(c):
        # print c
        s = ('\t'.join([str(s) if type(s) in (int,) else s for s in c]) + '\n').encode('utf8')
        out.write(s)

    keys = ['sitename', 'showurl', 'comtcount', 'showpraise', 'comtcount']

    write(keys + ['group', 'tag'])

    for line in open('/private/tmp/datas'):
        d = json.loads(line)
        if 'html' in d:
            url, html = d['url'], d['html']

            try:
                sites = json.loads(BeautifulSoup(html).find('body').text)

                m = re.search('tradeid=(\d+)&childid=(\d+)', url)
                tradid, childid = m.groups()
                trad = categories[tradid]['name']
                child = categories[tradid]['cats'][childid]

                for site in sites['data']['page']['result']:
                    write([site[k] for k in keys] + [trad, child])
                    jump.write(site['jumpurl'] + '\n')

            except Exception as e:
                print url, 'error', e

    out.close()
    jump.close()


if __name__ == '__main__':
    parse_result()
    # gen_download_urls()
    pass
# download_list()
